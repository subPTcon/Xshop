local stockKey = KEYS[1]
local reserveKey = KEYS[2]

local count = tonumber(ARGV[1])

-- 只有预占记录存在才补偿
if redis.call('EXISTS', reserveKey) == 0 then
    return 0
end

redis.call(
    'INCRBY',
    stockKey,
    count
)

redis.call(
    'DEL',
    reserveKey
)

return 1