require 'socket'
require 'multicast'

s = UDPSocket.open
r = MulticastSocket.new
r.subscribe("239.0.0.1", 8888)

loop do
    msg = r.recv(8192)
    if rand(100) > 50
      s.send(msg, 0, "239.0.0.1", 9999)
      puts "message '#{msg}' sent"
    else
      puts "message '#{msg}' dropped"
    end
end
