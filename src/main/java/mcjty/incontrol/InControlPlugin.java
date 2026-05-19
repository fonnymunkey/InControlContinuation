package mcjty.incontrol;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.Map;

@IFMLLoadingPlugin.Name("InControl")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class InControlPlugin implements IFMLLoadingPlugin {
	
	public InControlPlugin() {
		MixinBootstrap.init();
		MixinExtrasBootstrap.init();
		
		FermiumRegistryAPI.enqueueMixin(false, "mixins.incontrol.vanilla.json");
		FermiumRegistryAPI.enqueueMixin(true, "mixins.incontrol.mcjtytools.json");
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