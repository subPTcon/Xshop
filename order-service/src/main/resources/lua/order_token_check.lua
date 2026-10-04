local token = redis.call('GET', KEYS[1])

if not token then
    return 0
end

if token ~= ARGV[1] then
    return -1
end

redis.call('DEL', KEYS[1])

return 1