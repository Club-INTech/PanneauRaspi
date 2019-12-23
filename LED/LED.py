import sys
import board
import neopixel
import socket
import signal as sgl
import os
import smbus


def terminate(signalNumber, frame):
	pixels.fill((0, 0, 0))
	pixels.deinit()
	i2c.write_byte(0x71, 0x76)
	exit(0)


adr = 0x71

# sudo pip3 install rpi_ws281x adafruit-circuitpython-neopixel
port = int(sys.argv[1])
ledCount = int(sys.argv[2])
pixels = neopixel.NeoPixel(board.D18, ledCount, brightness=0.1)

# led server
connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
connection.bind(('', port))
connection.listen(5)

sgl.signal(sgl.SIGTERM, terminate)
sgl.signal(sgl.SIGUSR1, terminate)
PID = open("/home/pi/panneauRaspi/LED/PID", "w")
PID.write(str(os.getpid()) + "\n")
PID.close()
# os.system('sudo echo "' + os.getpid() + '"\n >/home/pi/panneauRaspi/LED/PID')
i2c = smbus.SMBus(1);

while True:
	client, info = connection.accept()
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
			if command == "fill":
				if len(args) == 3:
					r = int(255*float(args[0]))
					g = int(255*float(args[1]))
					b = int(255*float(args[2]))
					pixels.fill((r, g, b))
			elif command == "set":
				if len(args) == 4:
					index = int(args[0])
					r = int(255*float(args[1]))
					g = int(255*float(args[2]))
					b = int(255*float(args[3]))
					pixels[index] = (r, g, b)
			elif command == "range":
				if len(args) == 5:
					start = int(args[0])
					end = int(args[1])
					r = int(255*float(args[2]))
					g = int(255*float(args[3]))
					b = int(255*float(args[4]))
					for index in range(start, end+1):
						pixels[index] = (r, g, b)
			elif command == "update":
				pixels.show()
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
		except Exception as e:
			print(e)
