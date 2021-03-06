package de.hellfirepvp.nms.v1_12_R1;

import de.hellfirepvp.nms.BiomeMetaProvider;
import de.hellfirepvp.nms.NMSReflector;

import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.EnumCreatureType;
import net.minecraft.server.v1_12_R1.WeightedRandom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the CustomMobs Plugin
 * The plugin can be found at: https://www.spigotmc.org/resources/custommobs.7339
 * Class: BiomeMetaProviderImpl
 * Created by HellFirePvP
 * Date: 23.06.2016 / 16:23
 */
public class BiomeMetaProviderImpl implements BiomeMetaProvider {

//    @Override
//    public List getBiomes() {
//        return new LinkedList<>(BiomeBase.i);
//    }

    @Override
    public Map<String, Map<String, List<BiomeMetaProvider.NMSBiomeMetaLink>>> getSortedBiomeMeta() {
        Map<String, Map<String, List<BiomeMetaProvider.NMSBiomeMetaLink>>> dataOutput = new HashMap<>();
		for (BiomeBase base : (Iterable<BiomeBase>) BiomeBase.i) {

            if(base == null) continue;

            String name = base.toString();
            Map<String, List<BiomeMetaProvider.NMSBiomeMetaLink>> specData = new HashMap<>();
            for(EnumCreatureType type : EnumCreatureType.values()) {
                if (type == null) continue;

                List biomeMetaList = base.getMobs(type);
                specData.put(type.name(), getNMSRepresentation(biomeMetaList));
            }
            dataOutput.put(name, specData);
        }
        return dataOutput;
    }

    private List<BiomeMetaProvider.NMSBiomeMetaLink> getNMSRepresentation(List metas) {
        List<BiomeMetaProvider.NMSBiomeMetaLink> biomeMetaLinks = new LinkedList<>();
        for(Object objMeta : metas) {
            if(objMeta == null || !(objMeta instanceof BiomeBase.BiomeMeta)) continue;

            BiomeBase.BiomeMeta meta = (BiomeBase.BiomeMeta) objMeta;
            int weightedChance = NMSReflector.getField("a", WeightedRandom.WeightedRandomChoice.class, meta, int.class);
            String typeStr = getEntityTypeString(meta.b);
            if(typeStr == null) continue;
            biomeMetaLinks.add(new BiomeMetaProvider.NMSBiomeMetaLink(weightedChance, meta.c, meta.d, typeStr));
        }
        return biomeMetaLinks;
    }

    private String getEntityTypeString(Class<?> entityClass) {
        Map classToString = NMSReflector.getField("d", EntityTypes.class, null, Map.class);
        return (String) classToString.get(entityClass);
    }

    private Class<? extends EntityInsentient> getEntityTypeClass(String name) {
        Map stringToClass = NMSReflector.getField("c", EntityTypes.class, null, Map.class);
        try {
            return (Class<? extends EntityInsentient>) stringToClass.get(name);
        } catch (Throwable tr) {
            return null;
        }
    }

    @Override
    public boolean applyBiomeData(Map<String, Map<String, List<BiomeMetaProvider.NMSBiomeMetaLink>>> data) {
        try {
            for(String biomeName : data.keySet()) {
                if(biomeName == null) continue;
                BiomeBase base = getBiomeBaseByName(biomeName);
                if(base == null) continue;

                Map<String, List<BiomeMetaProvider.NMSBiomeMetaLink>> specData = data.get(biomeName);
                for(String target : specData.keySet()) {
                    if(target == null) continue;
                    EnumCreatureType type;
                    try {
                        type = EnumCreatureType.valueOf(target);
                    } catch (Throwable tr) {
                        continue;
                    }

                    if(type == null) continue;

                    List<BiomeBase.BiomeMeta> metas = getMetaList(specData.get(target));

                    List originalBiomeMetas = base.getMobs(type);
                    originalBiomeMetas.clear();
                    originalBiomeMetas.addAll(metas);
                }
            }

            return true;
        } catch (Throwable tr) {
            return false;
        }
    }

    private List<BiomeBase.BiomeMeta> getMetaList(List<BiomeMetaProvider.NMSBiomeMetaLink> links) {
        List<BiomeBase.BiomeMeta> metas = new LinkedList<>();
        for(BiomeMetaProvider.NMSBiomeMetaLink link : links) {
            if(link == null) continue;

            Class<? extends EntityInsentient> entityClass = getEntityTypeClass(link.entityClassStr);

            if(entityClass == null) continue;
            metas.add(new BiomeBase.BiomeMeta(entityClass, link.weightedMobChance, link.minimumCount, link.maximumCount));
        }
        return metas;
    }

    private BiomeBase getBiomeBaseByName(String name) {
		for (BiomeBase base : (Iterable<BiomeBase>) BiomeBase.i) {
            if(base == null) continue;
            if(base.toString().equalsIgnoreCase(name)) return base;
        }
        return null;
    }

	@Override
	public List getBiomes() {
		// TODO Auto-generated method stub
		return null;
	}
}
