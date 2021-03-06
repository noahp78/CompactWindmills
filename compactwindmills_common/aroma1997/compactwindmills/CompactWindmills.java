/*******************************************************************************
 * Copyright (c) 2013 Aroma1997.
 * All rights reserved. This program and other files related to this program are
 * licensed with a extended GNU General Public License v. 3
 * License informations are at:
 * https://github.com/Aroma1997/CompactWindmills/blob/master/license.txt
 ******************************************************************************/

package aroma1997.compactwindmills;


import ic2.api.item.Items;

import java.io.File;
import java.util.logging.Level;

import aroma1997.compactwindmills.helpers.LogHelper;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Reference.ModID, name = Reference.ModName, version = Reference.Version, dependencies = "required-after:IC2")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
/**
 * 
 * @author Aroma1997
 *
 */
public class CompactWindmills {
	
	@Instance(Reference.ModID)
	public static CompactWindmills instance;
	
	@SidedProxy(clientSide = "aroma1997.compactwindmills.ClientProxy", serverSide = "aroma1997.compactwindmills.CommonProxy")
	public static CommonProxy proxy;
	
	private static int blockID;
	
	public static Block windMill;
	
	public static final CreativeTabs creativeTabCompactWindmills = new CreativeTabCompactWindmills(
		"creativeTabCW");
	
	public static final int updateTick = 64;
	
	public static boolean vanillaIC2Stuff;
	
	public static boolean debugMode;
	
	private Configuration config;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LogHelper.init();
		if (Reference.Version == "@VERSION" + "@") {
			debugMode = true;
			LogHelper.log(Level.INFO, "Turning on debug mode.");
		}
		File tmpFile = new File(event.getModConfigurationDirectory(), "CompactWindmills.cfg");
		File configFile = new File(new File(event.getModConfigurationDirectory(), "aroma1997"), "CompactWindmills.cfg");
		if (tmpFile.exists()) {
			LogHelper.log(Level.INFO, "Moving old config file to aroma1997 folder.");
			tmpFile.renameTo(configFile);
		}
		Configuration config = new Configuration(
			configFile);
		this.config = config;
		config.load();
		Property block = config.getBlock("CompactWindmill", 2790);
		block.comment = "This is the id of the Compact Windmill Blocks.";
		blockID = block.getInt(2790);
		RotorType.getConfigs(config);
		Property vanillaIC2 = config.get(Configuration.CATEGORY_GENERAL, "useIC2Stuff", false);
		vanillaIC2.comment = "This defines if this mod just acts as a compact version of the vanilla IC2 Windmill or not."
			+ "'true' means IC2 version, 'false' means, the mod will change some stuff in its own windmills (recommended)"
			+ "This changes for example if the windmills require a rotor or if the wind strength is variable. This will also change the recipe.";
		vanillaIC2Stuff = vanillaIC2.getBoolean(false);
		
		config.save();
		
		RotorType.initRotors();
		
		windMill = new BlockCompactWindmill(blockID);
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		
		GameRegistry.registerBlock(windMill, ItemCompactWindMill.class, "blockCompactWindmill");
		for (WindType typ : WindType.values()) {
			GameRegistry.registerTileEntity(typ.claSS, typ.tileEntityName());
		}
		GameRegistry.addShapedRecipe(new ItemStack(windMill, 1, WindType.ELV.ordinal()), " W ", "WTW", " W ", 'W',
			Items.getItem("windMill"), 'T', Items.getItem("lvTransformer"));
		GameRegistry.addShapedRecipe(new ItemStack(windMill, 1, WindType.LV.ordinal()), " W ", "WTW", " W ", 'W',
			new ItemStack(windMill, 1, 0), 'T', Items.getItem("transformerUpgrade"));
		GameRegistry.addShapedRecipe(new ItemStack(windMill, 1, WindType.MV.ordinal()), " W ", "WTW", " W ", 'W',
			new ItemStack(windMill, 1, 1), 'T', Items.getItem("transformerUpgrade"));
		GameRegistry.addShapedRecipe(new ItemStack(windMill, 1, WindType.HV.ordinal()), " W ", "WTW", " W ", 'W',
			new ItemStack(windMill, 1, 2), 'T', Items.getItem("transformerUpgrade"));
		GameRegistry.addShapedRecipe(new ItemStack(windMill, 1, WindType.EV.ordinal()), " W ", "WTW", " W ", 'W',
			new ItemStack(windMill, 1, 3), 'T', Items.getItem("transformerUpgrade"));
		if (! vanillaIC2Stuff) {
			GameRegistry.addRecipe(new ItemStack(RotorType.CARBON.getItem()), "CCC", "CMC", "CCC",
				'C', Items.getItem("carbonPlate"), 'M', Items.getItem("machine"));
			GameRegistry.addRecipe(new ItemStack(RotorType.ALLOY.getItem()), "AAA", "AMA", "AAA",
				'A', Items.getItem("advancedAlloy"), 'M', Items.getItem("machine"));
			GameRegistry.addRecipe(new ItemStack(RotorType.WOOD.getItem()), "PSP", "SIS", "PSP",
				'S', new ItemStack(Item.stick), 'I', Items.getItem("plateiron"), 'p',
				new ItemStack(Block.planks));
			GameRegistry.addRecipe(new ItemStack(RotorType.IRIDIUM.getItem()), " I ", "IRI", " I ",
				'I', Items.getItem("iridiumPlate"), 'R', new ItemStack(RotorType.CARBON.getItem()));
			GameRegistry.addRecipe(new ItemStack(RotorType.WOOL.getItem()), "SWS", "WRW", "SWS",
				'S', new ItemStack(Item.silk), 'W', new ItemStack(Block.cloth), 'R', new ItemStack(
					RotorType.WOOD.getItem()));
		}
		
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.registerRotorRenderer(config);
		if (!ModLoader.isModLoaded("Aroma1997Core")) CoreReminder.init(Reference.ModName);
	}
}
