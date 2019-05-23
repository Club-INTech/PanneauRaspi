import sys
import board
import neopixel
import socket
import signal as sgl


def terminate(signalNumber, frame):
	pixels.fill((0, 0, 0))
	pixels.deinit()
	exit(0)


# sudo pip3 install rpi_ws281x adafruit-circuitpython-neopixel
port = int(sys.argv[1])
ledCount = int(sys.argv[2])
pixels = neopixel.NeoPixel(board.D18, ledCount, brightness=0.1)

# led server
connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
connection.bind(('', port))
connection.listen(5)

sgl.signal(sgl.SIGTERM, terminate)

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
		except Exception as e:
			print(e)
