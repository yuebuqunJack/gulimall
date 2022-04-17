package com.spower.gulimall.search.controller;

import com.spower.common.vo.search.SearchParam;
import com.spower.common.vo.search.SearchResult;
import com.spower.gulimall.search.service.impl.MallSearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 商城检索
 * @Author: jackc
 * @Date: 2022/04/03 23:51
 */
@Controller
public class ElasticSearchController {

    @Autowired
    MallSearchServiceImpl mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {

        param.set_queryString(request.getQueryString());
        //根据页面传递过来的查询参数去ES中检索商品
        SearchResult result = mallSearchService.search(param);
        //检索的结果返回到前端渲染
        model.addAttribute("result", result);
        return "list";
    }
}
