require 'socket'

s = UDPSocket.open
s.send(ARGV.join(' '), 0, "239.0.0.1", 8888)
s.close
