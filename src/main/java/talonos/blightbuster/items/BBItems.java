package talonos.blightbuster.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BBItems {
	public static Item purityFocus;
	public static Item silverPotion;
	public static Item worldTainter;
	public static Item worldSuperTainter;
	public static Item worldOreKiller;
	public static ItemBlock dawnChargerItem;
	public static Item nodeConverter;
	public static Item tunerFocus;
	public static Item chunkTainter;
	public static Item boundRing;
	public static Item biomeHelper;

	public static void init() {
		purityFocus = new ItemPurityFocus();
		silverPotion = new ItemSilverPotion();
		worldTainter = new ItemWorldTainter();
		worldOreKiller = new ItemWorldOreKiller();
		worldSuperTainter = new ItemSuperTestWorldTainter();
		worldSuperTainter = new ItemAlienTome();
		nodeConverter = new ItemNodeConverter();
		chunkTainter = new ItemChunkTainter();
		boundRing = new ItemBoundRing();
		biomeHelper = new ItemBiomeHelper();
	}
}
