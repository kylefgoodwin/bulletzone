//package edu.unh.cs.cs619.bulletzone.ui;
//
//import android.app.Activity;
//import android.graphics.Color;
//import android.graphics.PorterDuff;
//import android.widget.Button;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.Objects;
//
//import edu.unh.cs.cs619.bulletzone.R;
//import edu.unh.cs.cs619.bulletzone.PlayerData;
//import edu.unh.cs.cs619.bulletzone.rest.GridUpdateEvent;
//
///**
// * Controller in charge of keeping track of and setting unit and material selections
// */
//public class PlayerViewController {
//    private static volatile PlayerViewController INSTANCE = null;
//
//    private long lastSwitched;
//    private long lastSelected;
//
//    public static PlayerViewController getInstance() {
//        if (INSTANCE == null) {
//            synchronized (PlayerViewController.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = new PlayerViewController();
//                }
//            }
//        }
//        return INSTANCE;
//    }
//    private final PlayerData playerData;
//
//    private PlayerViewController() {
//        this.playerData = PlayerData.getPlayerData();
//    }
//
//    /**
//     * sets all button groups with the current selections
//     * @param activity represents the activity in which the buttons reside
//     */
//    public void setAllGroups(Activity activity) {
//        materialGroup(activity);
//        unitGroup(activity);
//    }
//
//    /**
//     * sets tank as current selection in unit group
//     * @param activity represents the activity in which the buttons reside
//     */
//    public void buttonTank(Activity activity) {
//        if (System.currentTimeMillis() - lastSwitched >= 500) {
//            playerData.setCurId(playerData.getTankId());
//            unitGroup(activity);
//            lastSwitched = System.currentTimeMillis();
//            PlayerData.getPlayerData().setCurrentMap(PlayerData.getPlayerData().getTankMap());
////            EventBus.getDefault().post(new GridUpdateEvent(true));
//        }
//    }
//
//    /**
//     * sets builder as current selection in unit group
//     * @param activity represents the activity in which the buttons reside
//     */
//    public void buttonBuilder(Activity activity) {
//        if (System.currentTimeMillis() - lastSwitched >= 500) {
//            playerData.setCurId(playerData.getBuilderId());
//            unitGroup(activity);
//            lastSwitched = System.currentTimeMillis();
//            PlayerData.getPlayerData().setCurrentMap(PlayerData.getPlayerData().getBuilderMap());
////            EventBus.getDefault().post(new GridUpdateEvent(true));
//        }
//    }
//
//    /**
//     * sets destructible wall as the current selection in meterial groupd
//     * @param activity represents the activity in which the buttons reside
//     */
//    public void buttonDesWall(Activity activity) {
//        if (System.currentTimeMillis() - lastSelected >= 500) {
//            playerData.setCurEntity("destructibleWall");
//            materialGroup(activity);
//            lastSelected = System.currentTimeMillis();
//        }
//    }
//
//    /**
//     * sets indestructible wall as current selection in material group
//     * @param activity represents the activity in which the buttons reside
//     */
//    public void buttonIndWall(Activity activity) {
//        if (System.currentTimeMillis() - lastSelected >= 500) {
//            playerData.setCurEntity("indestructibleWall");
//            materialGroup(activity);
//        }
//    }
//
//    public void buttonFacility(Activity activity) {
//        //Step 2 for facility
//        if (System.currentTimeMillis() - lastSelected >= 500) {
//            playerData.setCurEntity("facility");
//            materialGroup(activity);
//        }
//    }
//
//    /**
//     * will update unit group button visual selections based on what is currently selected
//     * @param activity represents the activity in which the buttons reside
//     */
//    public void unitGroup(Activity activity) {
//        int cur = (int) playerData.getCurId();
//        int tank = (int) playerData.getTankId();
//        int build = (int) playerData.getBuilderId();
//
//        Button btnTank = activity.findViewById(R.id.buttonTank);
//        Button btnBuilder = activity.findViewById(R.id.buttonBuilder);
//
//        Button btnIndWall = activity.findViewById(R.id.buttonIndWall);
//        Button btnDesWall = activity.findViewById(R.id.buttonDesWall);
//        Button btnFacility = activity.findViewById(R.id.buttonFacility);
//        Button btnBuild = activity.findViewById(R.id.buttonBuildOrDismantle);
//
//        if (cur == tank) {
//            btnTank.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//            btnBuilder.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//
//            btnDesWall.setEnabled(false);
//            btnDesWall.getBackground().setColorFilter(Color.rgb(170, 74, 68), PorterDuff.Mode.MULTIPLY);
//
//            btnIndWall.setEnabled(false);
//            btnIndWall.getBackground().setColorFilter(Color.rgb(170, 74, 68), PorterDuff.Mode.MULTIPLY);
//
//            btnBuild.setEnabled(false);
//            btnBuild.getBackground().setColorFilter(Color.rgb(170, 74, 68), PorterDuff.Mode.MULTIPLY);
//
//            btnFacility.setEnabled(false);
//            btnFacility.getBackground().setColorFilter(Color.rgb(170, 74, 68), PorterDuff.Mode.MULTIPLY);
//
//        }
//        if (cur == build) {
//            btnTank.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//            btnBuilder.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//
//            btnDesWall.setEnabled(true);
//            btnIndWall.setEnabled(true);
//            btnBuild.setEnabled(true);
//            btnBuild.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//            btnFacility.setEnabled(true);
//
//            materialGroup(activity);
//        }
//    }
//
//    /**
//     * will update material group button visual selections based on what is currently selected
//     * @param activity represents the activity in which the buttons reside
//     */
//    public void materialGroup(Activity activity) {
//        String cur = playerData.getCurEntity();
//
//        Button btnDesWall = activity.findViewById(R.id.buttonDesWall);
//        Button btnIndWall = activity.findViewById(R.id.buttonIndWall);
//        Button btnFacility = activity.findViewById(R.id.buttonFacility);
//
//        if (Objects.equals(cur, "destructibleWall")) {
//            btnDesWall.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//            btnIndWall.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//            btnFacility.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//        }
//        if (Objects.equals(cur, "indestructibleWall")) {
//            btnDesWall.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//            btnIndWall.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//            btnFacility.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//        }
//        if (Objects.equals(cur, "facility")) {
//            btnDesWall.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//            btnIndWall.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
//            btnFacility.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//        }
//    }
//}
