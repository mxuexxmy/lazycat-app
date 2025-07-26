package xyz.mxue.lazycatapp.converter;

import xyz.mxue.lazycatapp.entity.Category;

public class CategoryConvert {

    public static Category convert(Category zh, Category en) {
        Category result = new Category();
        result.setId(zh.getId());
        result.setName(zh.getName());
        result.setIcon(zh.getIcon());
        result.setEnglishName(en.getName());
        return result;
    }

}
