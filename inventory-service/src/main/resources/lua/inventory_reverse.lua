-- KEYS[1] 可售库存key
-- KEYS[2] 订单SKU预占key
--
-- ARGV[1] 购买数量
-- ARGV[2] 预占key过期时间（秒）

local stockKey = KEYS[1]
local reserveKey = KEYS[2]

local count = tonumber(ARGV[1])
local expireSeconds = tonumber(ARGV[2])

-- 1. 是否已经预占
if redis.call('EXISTS', reserveKey) == 1 then
    return 2
end

-- 2. 查询当前库存
local stock = redis.call('GET', stockKey)

-- 库存key不存在
if not stock then
    return -1
end

stock = tonumber(stock)

-- 3. 库存不足
if stock < count then
    return 0
end

-- 4. 原子扣减库存
redis.call('DECRBY', stockKey, count)

-- 5. 写预占标记
redis.call(
    'SET',
    reserveKey,
    count,
    'EX',
    expireSeconds
)

return 1