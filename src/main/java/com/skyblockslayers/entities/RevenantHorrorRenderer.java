package com.skyblockslayers.entities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public class RevenantHorrorRenderer extends HumanoidMobRenderer<RevenantHorror, RevenantHorrorModel> {
    public RevenantHorrorRenderer(EntityRendererProvider.Context context) {
        super(context, new RevenantHorrorModel(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(RevenantHorror entity) {
        return ResourceLocation.fromNamespaceAndPath("skyblockslayers", "textures/entity/revenant_horror.png");
    }

    @Override
    protected void renderNameTag(RevenantHorror entity, Component displayName, PoseStack poseStack,
                                 MultiBufferSource buffer, int packedLight, float partialTick) {
        super.renderNameTag(entity, displayName, poseStack, buffer, packedLight, partialTick);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.26F, 0.0F);
        super.renderNameTag(entity,
                Component.literal("SLAYER BOSS").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                poseStack, buffer, packedLight, partialTick);
        poseStack.popPose();
    }
}