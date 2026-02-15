package mcjty.incontrol;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.Name("InControl")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class InControlPlugin implements IFMLLoadingPlugin {
	
	public InControlPlugin() {
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.incontrol.vanilla.json");
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}
	
	@Override
	public String getModContainerClass() {
		return null;
	}
	
	@Override
	public String getSetupClass() {
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) { }
	
	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}