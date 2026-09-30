-- KEYS[1] = inventory:{skuId}
-- KEYS[2] = inventory:reserved:{orderNo}:{skuId}
-- ARGV[1] = count

local stockKey = KEYS[1]
local reserveKey = KEYS[2]

local count = tonumber(ARGV[1])

-- Redis库存key存在才增加
if redis.call('EXISTS', stockKey) == 1 then
    redis.call('INCRBY', stockKey, count)
else
    -- 返回特殊值，Java端回源MySQL重建
    redis.call('DEL', reserveKey)
    return -1
end

-- 删除预占标记
redis.call('DEL', reserveKey)

return 1