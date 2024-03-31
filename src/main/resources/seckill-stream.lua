local voucherId = ARGV[1];

local userId = ARGV[2]

local orderId = ARGV[3]

local storeKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order' .. voucherId
if (tonumber(redis.call('get', storeKey)) <= 0) then
    return 1;
end

if (redis.call('sismember', orderKey, userId) == 1) then
    return 2;
end

redis.call('incrby', storeKey, -1)

redis.call('sadd', orderKey, userId)

-- 发送消息 ,*表示由redis生成消息主键  XGROUP CREATE stream.orders g1 0 MKSTREAM
redis.call('XADD','stream.orders','*','userId',userId,'voucherId',voucherId,'id',orderId)

return 0;