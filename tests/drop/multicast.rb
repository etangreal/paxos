require 'socket'
require 'ipaddr'

class MulticastSocket < UDPSocket
  def subscribe(addr, port)
    ip =  IPAddr.new(addr).hton + IPAddr.new("0.0.0.0").hton
    self.setsockopt(Socket::IPPROTO_IP, Socket::IP_ADD_MEMBERSHIP, ip)
    self.bind(Socket::INADDR_ANY, port)
  end
end
