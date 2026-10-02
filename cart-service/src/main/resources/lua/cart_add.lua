-- KEYS[1] = cart:{userId}
--
-- ARGV[1] = skuId
-- ARGV[2] = 本次增加数量
-- ARGV[3] = 单SKU最大数量
-- ARGV[4] = 购物车最大SKU种类数
-- ARGV[5] = TTL秒数

local cartKey = KEYS[1]

local skuId = ARGV[1]
local addCount = tonumber(ARGV[2])
local maxItemCount = tonumber(ARGV[3])
local maxCartItems = tonumber(ARGV[4])
local expireSeconds = tonumber(ARGV[5])

-- 查询当前SKU数量
local currentCount = redis.call(
    'HGET',
    cartKey,
    skuId
)

-- SKU还不存在
if not currentCount then

    -- 检查购物车SKU种类数
    local itemSize = redis.call(
        'HLEN',
        cartKey
    )

    if itemSize >= maxCartItems then
        return -2
    end

    currentCount = 0
else
    currentCount = tonumber(currentCount)
end

local newCount =
        currentCount + addCount

-- 超过单SKU最大数量
if newCount > maxItemCount then
    return -1
end

-- 写入最终数量
redis.call(
    'HSET',
    cartKey,
    skuId,
    newCount
)

-- 刷新购物车TTL
redis.call(
    'EXPIRE',
    cartKey,
    expireSeconds
)

return newCount