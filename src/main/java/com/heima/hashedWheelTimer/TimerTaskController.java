package com.heima.hashedWheelTimer;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * 延迟队列测试 API
 * </pre>
 *
 * @author loopy_y
 * @since 2022/6/14
 */
@Slf4j
@RestController
@RequestMapping("/timer")
@Api(tags = "延迟队列API")
public class TimerTaskController {

    @GetMapping("/add")
    public ApiResult<Boolean> add(@RequestParam("orderId") String orderId) {
        OrderCancelTimer.addNewTimeout(new OrderCancelTimerTask(orderId), orderId);
        return ApiResult.ok();
    }

    @GetMapping("/delete")
    public ApiResult<Boolean> delete(@RequestParam("orderId") String orderId) {
        boolean delStatus = OrderCancelTimer.delTimeout(orderId);
        return ApiResult.success(delStatus);
    }

}
