import sys
import board
import neopixel

# sudo pip3 install rpi_ws281x adafruit-circuitpython-neopixel
color = sys.argv[2:]
pixels = neopixel.NeoPixel(board.D18, 30)
pixels.fill((color[0], color[1], color[2]))
