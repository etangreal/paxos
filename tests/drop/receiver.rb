require 'multicast'

r = MulticastSocket.new
r.subscribe("239.0.0.1", 9999)

loop do
    puts r.recv(8192)
end
