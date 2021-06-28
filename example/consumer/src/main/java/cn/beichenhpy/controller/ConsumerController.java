package cn.beichenhpy.controller;

import cn.beichenhpy.modal.Article;
import cn.beichenhpy.modal.Comment;
import cn.beichenhpy.service.ConsumerService;
import cn.beichenhpy.service.feign.ProviderFeignService;
import cn.beichenhpy.utils.asserts.AssertToolkit;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ConsumerController {

    private final ProviderFeignService providerFeignService;
    private final ConsumerService consumerService;
    /**
     * 配置异常数 fallback生效
     * 注意：fallback的方法参数必须与修饰的相同，可以添加异常类
     * @param aid 参数
     * @return 返回值
     */
    @GetMapping("/info/{aid}")
    @SentinelResource(value = "ArticleInfo",fallback = "fallback")
    public ResponseEntity<Article> info(@PathVariable("aid") String aid) {
        if ("error".endsWith(aid)){
            throw new RuntimeException("降级");
        }
        ResponseEntity<List<Comment>> response = providerFeignService.getCommentInfoByArticleId(aid);
        AssertToolkit.feignResponseFailException(response.getStatusCode(),null);
        Article article = consumerService.getById(aid);
        article.setComments(response.getBody());
        return ResponseEntity.ok(article);
    }

    /**
     * 异常埋点的fallback方法
     * @param aid 与原方法要相同
     * @param throwable 异常
     * @return 返回值
     */
    public ResponseEntity<String> fallback(String aid,Throwable throwable) {
        return ResponseEntity.badRequest().body("埋点"+throwable.getMessage());
    }
}
