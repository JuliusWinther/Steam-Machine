# Set DNS servers to Google DNS (IPv4 and IPv6)
$dnsServers = @("8.8.8.8", "8.8.4.4", "2001:4860:4860::8888", "2001:4860:4860::8844")

# Set DNS servers for IPv4
Set-DnsClientServerAddress -InterfaceAlias "*" -ServerAddresses $dnsServers[0..1]

# Set DNS servers for IPv6
Set-DnsClientServerAddress -InterfaceAlias "*" -ServerAddresses $dnsServers[2..3]

Write-Host "DNS settings changed to Google DNS."
