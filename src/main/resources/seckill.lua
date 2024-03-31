local voucherId = ARGV[1];

local userId = ARGV[2]

local storeKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order' .. voucherId
if (tonumber(redis.call('get', storeKey)) <= 0) then
    return 1;
end

if(redis.call('sismember',orderKey,userId)==1) then
    return 2;
end

redis.call('incrby',storeKey,-1)

redis.call('sadd',orderKey,userId)

return 0;