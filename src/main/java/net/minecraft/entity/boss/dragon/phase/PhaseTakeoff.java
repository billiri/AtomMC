package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseTakeoff extends PhaseBase {
    private boolean firstTick;
    private Path currentPath;
    private Vec3d targetLocation;

    public PhaseTakeoff(EntityDragon dragonIn) {
        super(dragonIn);
    }

    public void doLocalUpdate() {
        if (!this.firstTick && this.currentPath != null) {
            BlockPos blockpos = this.dragon.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
            double d0 = this.dragon.getDistanceSqToCenter(blockpos);

            if (d0 > 100.0D) {
                this.dragon.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
            }
        } else {
            this.firstTick = false;
            this.findNewTarget();
        }
    }

    public void initPhase() {
        this.firstTick = true;
        this.currentPath = null;
        this.targetLocation = null;
    }

    private void findNewTarget() {
        int i = this.dragon.initPathPoints();
        Vec3d vec3d = this.dragon.getHeadLookVec(1.0F);
        int j = this.dragon.getNearestPpIdx(-vec3d.x * 40.0D, 105.0D, -vec3d.z * 40.0D);

        if (this.dragon.getFightManager() != null && this.dragon.getFightManager().getNumAliveCrystals() > 0) {
            j = j % 12;

            if (j < 0) {
                j += 12;
            }
        } else {
            j = j - 12;
            j = j & 7;
            j = j + 12;
        }

        this.currentPath = this.dragon.findPath(i, j, (PathPoint) null);

        if (this.currentPath != null) {
            this.currentPath.incrementPathIndex();
            this.navigateToNextPathNode();
        }
    }

    private void navigateToNextPathNode() {
        Vec3d vec3d = this.currentPath.getCurrentPos();
        this.currentPath.incrementPathIndex();
        double d0;

        while (true) {
            d0 = vec3d.y + (double) (this.dragon.getRNG().nextFloat() * 20.0F);

            if (d0 >= vec3d.y) {
                break;
            }
        }

        this.targetLocation = new Vec3d(vec3d.x, d0, vec3d.z);
    }

    @Nullable
    public Vec3d getTargetLocation() {
        return this.targetLocation;
    }

    public PhaseList<PhaseTakeoff> getType() {
        return PhaseList.TAKEOFF;
    }
}