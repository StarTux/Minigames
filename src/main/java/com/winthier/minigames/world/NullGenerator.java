package com.winthier.minigames.world;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

@SuppressWarnings("deprecation")
public class NullGenerator extends ChunkGenerator {
        @Override
        public boolean canSpawn(World world, int x, int z) {
                return true;
        }

        @Override
        public byte[] generate(World world, Random random, int x, int z) {
                byte[] result = new byte[32768];
                return result;
        }

        @Override
        public byte[][] generateBlockSections(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biomes) {
                return null;
        }

        @Override
        public short[][] generateExtBlockSections(World world, Random random, int x, int z, ChunkGenerator.BiomeGrid biomes) {
                return null;
        }

        @Override
        public List<BlockPopulator> getDefaultPopulators(World world) {
                return Collections.<BlockPopulator>emptyList();
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
                return null;
        }
}
