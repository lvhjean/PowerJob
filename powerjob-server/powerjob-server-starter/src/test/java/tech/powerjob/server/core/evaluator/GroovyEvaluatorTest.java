package tech.powerjob.server.core.evaluator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.powerjob.common.serialize.JsonUtils;

import java.util.HashMap;

/**
 * @author Echo009
 * @since 2022/04/13
 */
public class GroovyEvaluatorTest {

    private final GroovyEvaluator groovyEvaluator = new GroovyEvaluator();

    private final HashMap<String, String> SIMPLE_CONTEXT = new HashMap<>();

    private final HashMap<String, String> COMPLEX_CONTEXT = new HashMap<>();
    private final HashMap<String, String> COMPLEX_CONTEXT2 = new HashMap<>();

    {
        // simple context
        // {"k1":"1","k2":"\"2\"","k3":"false","k4":"1.1"}
        SIMPLE_CONTEXT.put("k1", JsonUtils.toJSONString(1));
        SIMPLE_CONTEXT.put("k2", JsonUtils.toJSONString("2"));
        SIMPLE_CONTEXT.put("k3", JsonUtils.toJSONString(false));
        SIMPLE_CONTEXT.put("k4", JsonUtils.toJSONString(1.1d));

        // complex context
        // {"array":"[1,2,3,4,5]","obj":"{\"id\":\"e3\",\"value\":3,\"sub\":{\"id\":\"e2\",\"value\":2,\"sub\":{\"id\":\"e1\",\"value\":1,\"sub\":null}}}","map":"{\"e1\":{\"id\":\"e1\",\"value\":1,\"sub\":null}}"}
        COMPLEX_CONTEXT.put("array", JsonUtils.toJSONString(new int[]{1, 2, 3, 4, 5}));
        Element e1 = new Element("e1",1,null);
        Element e2 = new Element("e2",2,e1);
        Element e3 = new Element("e3",3,e2);
        COMPLEX_CONTEXT.put("obj", JsonUtils.toJSONString(e3));
        HashMap<String, Object> map = new HashMap<>();
        map.put("e1",e1);
        COMPLEX_CONTEXT.put("map",JsonUtils.toJSONString(map));

        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("compute","add");
        map2.put("num","7");        
        COMPLEX_CONTEXT2.put("initParams", JsonUtils.toJSONString(map2));

    }

    @Test
    public void testSimpleEval1() {
        Object res = groovyEvaluator.evaluate("var x = false; x;", null);
        Assertions.assertEquals(false, res);
    }

    @Test
    public void testSimpleEval11() {
        Object res = groovyEvaluator.evaluate("var x = false; return x;", null);
        Assertions.assertEquals(false, res);
    }

    @Test
    public void testSimpleEval2() {
        // inject simple context
        System.out.println("上下文信息:"+ JsonUtils.toJSONString(COMPLEX_CONTEXT2));
        Object res = groovyEvaluator.evaluate("var e3 = new groovy.json.JsonSlurper().parseText(context.initParams); e3.num>10;", COMPLEX_CONTEXT2);
        Boolean s = JsonUtils.parseObjectUnsafe(res.toString(), Boolean.class);
        Assertions.assertEquals(false, s);
    }


    @Test
    public void testSimpleEval3() {
        Object res = groovyEvaluator.evaluate("var res = new groovy.json.JsonSlurper().parseText(context.k3); res == false;", SIMPLE_CONTEXT);
        Assertions.assertEquals(true, res);
    }


    @Test
    public void testComplexEval1() {
        // array
        Object res = groovyEvaluator.evaluate("var res = new groovy.json.JsonSlurper().parseText(context.array) ; res[0] == 1;", COMPLEX_CONTEXT);
        Assertions.assertEquals(true, res);
        // map
        res = groovyEvaluator.evaluate("var map = new groovy.json.JsonSlurper().parseText(context.map); var e1 = map.e1; e1.value ",COMPLEX_CONTEXT);
        Assertions.assertEquals(1,res);
        // object
        res = groovyEvaluator.evaluate("var e3 = new groovy.json.JsonSlurper().parseText(context.obj); var e1 = e3.sub.sub; e1.value ",COMPLEX_CONTEXT);
        Assertions.assertEquals(1,res);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Element {

        private String id;

        private Integer value;

        private Element sub;

    }

}
