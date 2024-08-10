# Set DNS servers to Cloudflare (IPv4 and IPv6)
$dnsServers = @("1.1.1.1", "1.0.0.1", "2606:4700:4700::1111", "2606:4700:4700::1001")

# Set DNS servers for IPv4
Set-DnsClientServerAddress -InterfaceAlias "*" -ServerAddresses $dnsServers[0..1]

# Set DNS servers for IPv6
Set-DnsClientServerAddress -InterfaceAlias "*" -ServerAddresses $dnsServers[2..3]

Write-Host "DNS settings changed to Cloudflare."
