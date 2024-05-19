package com.changgou.goods.controller;

import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
@CrossOrigin
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public Result<List<Brand>> findAll() {
        return Result.ok("查询成功", brandService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Brand> findById(@PathVariable Integer id) {
        return Result.ok("查询成功", brandService.findById(id));
    }

    @PostMapping
    public Result add(@RequestBody Brand brand) {
        brandService.add(brand);
        return Result.ok("添加成功");
    }

    @PutMapping
    public Result update(@RequestBody Brand brand) {
        brandService.update(brand);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        brandService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping(value = "/search")
    public Result<List<Brand>> findList(@RequestBody(required = false) Brand brand) {
        List<Brand> list = brandService.findList(brand);
        return Result.ok("查询成功", list);
    }

    /***
     * 分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@PathVariable int page, @PathVariable int size) {
        // 分页查询
        PageInfo<Brand> pageInfo = brandService.findPage(page, size);
        return Result.ok("查询成功", pageInfo);
    }

    /***
     * 分页搜索实现
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@RequestBody(required = false) Brand brand, @PathVariable int page, @PathVariable int size) {
        // 执行搜索
        PageInfo<Brand> pageInfo = brandService.findPage(brand, page, size);
        return Result.ok("查询成功", pageInfo);
    }

}
