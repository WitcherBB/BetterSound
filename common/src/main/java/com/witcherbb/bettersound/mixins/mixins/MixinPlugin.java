package com.witcherbb.bettersound.mixins.mixins;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private boolean isFrameworkInstalled;
    @Override
    public void onLoad(String mixinPackage) {
        try {
            // 用与 loader 无关的 Constants 判断 mod 是否已加载（主类 BetterSound 只存在于 forge 模块）
            Class.forName("com.witcherbb.bettersound.Constants", false, this.getClass().getClassLoader());
            isFrameworkInstalled = true;
        } catch (Exception e) {
            isFrameworkInstalled = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return "bettersound.refmap.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isFrameworkInstalled; // this makes sure that forge's helpful mods not found screen shows up
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

}