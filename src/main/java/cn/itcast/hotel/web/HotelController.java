package cn.itcast.hotel.web;


import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/hotel")
@RestController
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams){
        return hotelService.search(requestParams);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> filter(@RequestBody RequestParams requestParams){
        return hotelService.filters(requestParams);
    }

    @GetMapping("/suggestion")
    public List<String> getSuggestions(@RequestParam String key){
        return hotelService.getSuggestions(key);
    }


}
