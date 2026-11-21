package com.skyblockslayers.entities;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelPart;

public class RevenantHorrorModel extends AbstractZombieModel<RevenantHorror> {
    public RevenantHorrorModel(ModelPart root) {
        super(root);
    }

    @Override
    public boolean isAggressive(RevenantHorror entity) {
        return entity.isAggressive();
    }
}