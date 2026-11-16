package com.witcherbb.bettersound.fabric.data;

import com.witcherbb.bettersound.fabric.data.tags.BlockTagGenerator;
import com.witcherbb.bettersound.fabric.data.tags.ItemTagGenerator;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModelProvider::new);
        var blockTagGenerator = pack.addProvider(BlockTagGenerator::new);
        pack.addProvider((output, registriesFuture) ->
                new ItemTagGenerator(output, registriesFuture, blockTagGenerator));
    }
}
