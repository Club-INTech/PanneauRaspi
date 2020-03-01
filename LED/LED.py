import sys
import socket
import signal as sgl
import os
import Jetson.GPIO as GPIO
import smbus2 as bus


# I2C fonctionnel pour ce module d'affichage:
# https://learn.sparkfun.com/tutorials/using-the-serial-7-segment-display/all


def terminate(signalNumber, frame):
    # i2c.write_byte(0x71, 0x76)
    TCPconnection.close()
    UDPconnection.close()
    GPIO.cleanup()
    exit(0)


def ISR(channel):
    print("interrupting")
    if GPIO.input(channel) == GPIO.LOW:
        UDPconnection.sendto(b"JAUNE", ("localhost", UDPport))
    else:
        UDPconnection.sendto(b"BLEU", ("localhost", UDPport))


def i2cwrite(params):
    return i2c.i2c_rdwr(bus.i2c_msg.write(adr, params))


adr = 0x71  # adresse i2c du 7 segments
switch_pin = 7
blue_pin = 11
yellow_pin = 13
GPIO.setmode(GPIO.BOARD)

# sudo pip3 install rpi_ws281x adafruit-circuitpython-neopixel

TCPport = int(sys.argv[1])
UDPport = int(sys.argv[2])

# led server
TCPconnection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
TCPconnection.bind(('', TCPport))
TCPconnection.listen(5)

# switch server
UDPconnection = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

sgl.signal(sgl.SIGTERM, terminate)
sgl.signal(sgl.SIGUSR1, terminate)
PID = open("/home/intech/PanneauRaspi/LED/PID", "w")
PID.write(str(os.getpid()) + "\n")
PID.close()
# os.system('sudo echo "' + os.getpid() + '"\n >/home/pi/panneauRaspi/LED/PID')

i2c = bus.SMBus(1)
GPIO.setup(switch_pin, GPIO.IN, pull_up_down=GPIO.PUD_UP)  # les GPIO de Jetson ne permettent pas de configurer le pullup interne, il faut en faire un hardware
GPIO.add_event_detect(switch_pin, GPIO.BOTH, callback=ISR, bouncetime=10)
GPIO.setup((blue_pin, yellow_pin), GPIO.OUT, initial=GPIO.LOW)

i2cwrite([0x76])  # clear display

while True:
    try:
        print("waiting connection")
        client, info = TCPconnection.accept()
        print("connected")
        while True:
            try:
                data = client.recv(1024)
                if not data:
                    break
                message = data.decode('utf-8')
                print("Received: ", message)
                parts = message.split()
                if len(parts) == 0:
                    continue
                command = parts[0]
                args = parts[1:]
                if command == "set":
                    if len(args) == 1:
                        color = args[0]
                        if color == "BLEU":
                            GPIO.output(yellow_pin, GPIO.LOW)
                            GPIO.output(blue_pin, GPIO.HIGH)
                        elif color == "JAUNE":
                            GPIO.output(yellow_pin, GPIO.HIGH)
                            GPIO.output(blue_pin, GPIO.LOW)
                        else:
                            GPIO.output((yellow_pin, blue_pin), GPIO.LOW)
                    else:
                        GPIO.output((yellow_pin, blue_pin), GPIO.LOW)

                elif command == "score":
                    if len(args) == 1:
                        nb = int(args[0])
                        nb4 = nb % 10
                        nb //= 10
                        nb3 = nb % 10
                        nb //= 10
                        nb2 = nb % 10
                        nb1 = nb // 10
                        if nb1 >= 10:
                            print("erreur: on ne peut pas afficher plus de 4 chiffres")
                        else:
                            i2cwrite([0x79])  # curseur sur digit 0
                            i2cwrite([nb1, nb2, nb3, nb4])
                elif command == "segmentsBrightness":
                    if len(args) == 1:
                        if args[0] in range(256):
                            i2cwrite([0x7A, args[0]])
                        else:
                            print("Erreur valeur de luminosité doit être comprise entre 0 et 255")
                elif command == "segmentsClear":
                    i2cwrite([0x76])
            except Exception as e:
                print(e)
    except KeyboardInterrupt:
        terminate(0, 0)
