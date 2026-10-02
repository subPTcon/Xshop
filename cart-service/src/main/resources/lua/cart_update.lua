-- KEYS[1] = cart:{userId}
--
-- ARGV[1] = skuId
-- ARGV[2] = 最终数量
-- ARGV[3] = TTL秒数

local cartKey = KEYS[1]

local skuId = ARGV[1]
local count = tonumber(ARGV[2])
local expireSeconds = tonumber(ARGV[3])

-- 1. 判断SKU是否存在于购物车
if redis.call('HEXISTS', cartKey, skuId) == 0 then
    return 0
end

-- 2. 设置最终数量
redis.call(
    'HSET',
    cartKey,
    skuId,
    count
)

-- 3. 刷新购物车TTL
redis.call(
    'EXPIRE',
    cartKey,
    expireSeconds
)

return 1