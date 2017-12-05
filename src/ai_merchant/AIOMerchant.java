package ai_merchant;

import ai_merchant.state.MerchantTask;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;

@ScriptManifest(
        author = "XD123",
        name = "123Flip",
        version = 1.0,
        description = "AIO Flipping on the GE for profit",
        category = Category.MONEYMAKING)
public class AIOMerchant extends TaskScript {

    @Override
    public void onStart() {
        addNodes();
    }

    @Override
    public void onExit() {

    }

}

