import sys
import socket
import signal as sgl
import os
import Jetson.GPIO as GPIO


def terminate(signalNumber, frame):
	# i2c.write_byte(0x71, 0x76)
	TCPconnection.close()
	UDPconnection.close()
	GPIO.cleanup()
	exit(0)


def ISR(channel):
	if GPIO.input(channel) == GPIO.LOW:
		UDPconnection.sendto(b"JAUNE", ("localhost", UDPport))
	else:
		UDPconnection.sendto(b"BLEU", ("localhost", UDPport))


adr = 0x71  # adresse i2c du 7 segments
switch_pin = 0
blue_pin = 0
yellow_pin = 0
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
PID = open("/home/intech/panneauRaspi/LED/PID", "w")
PID.write(str(os.getpid()) + "\n")
PID.close()
# os.system('sudo echo "' + os.getpid() + '"\n >/home/pi/panneauRaspi/LED/PID')
# i2c = smbus.SMBus(1)

GPIO.setup(switch_pin, GPIO.IN)
GPIO.add_event_detect(switch_pin, GPIO.BOTH, callback=ISR, bouncetime=10)
GPIO.setup((blue_pin, yellow_pin), GPIO.OUT)

while True:
	client, info = TCPconnection.accept()
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
			"""
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
						i2c.write_byte_data(adr, 0x79, 0x00)  # on place le curseur sur le premier chiffre
						i2c.write_i2c_block_data(adr, nb1, [nb2, nb3, nb4])
			elif command == "segmentsBrightness":
				if len(args) == 1:
					if args[0] in range(256):
						i2c.write_byte_data(adr, 0x7A, args[0])
					else:
						print("Erreur valeur de luminosité doit être comprise entre 0 et 255")
			"""
		except Exception as e:
			print(e)
