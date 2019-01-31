/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2019. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import com.google.common.base.Objects;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.ItemTurtle;
import dan200.computercraft.shared.util.Holiday;
import dan200.computercraft.shared.util.HolidayUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class TurtleSmartItemModel implements IBakedModel, ISelectiveResourceReloadListener
{
    private static final Matrix4f s_identity, s_flip;

    static
    {
        s_identity = new Matrix4f();
        s_identity.setIdentity();

        s_flip = new Matrix4f();
        s_flip.setIdentity();
        s_flip.m11 = -1; // Flip on the y axis
        s_flip.m13 = 1; // Models go from (0,0,0) to (1,1,1), so push back up.
    }

    private static class TurtleModelCombination
    {
        public final ComputerFamily m_family;
        public final boolean m_colour;
        public final ITurtleUpgrade m_leftUpgrade;
        public final ITurtleUpgrade m_rightUpgrade;
        public final ResourceLocation m_overlay;
        public final boolean m_christmas;
        public final boolean m_flip;

        public TurtleModelCombination( ComputerFamily family, boolean colour, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, ResourceLocation overlay, boolean christmas, boolean flip )
        {
            m_family = family;
            m_colour = colour;
            m_leftUpgrade = leftUpgrade;
            m_rightUpgrade = rightUpgrade;
            m_overlay = overlay;
            m_christmas = christmas;
            m_flip = flip;
        }

        @Override
        public boolean equals( Object other )
        {
            if( other == this ) return true;
            if( !(other instanceof TurtleModelCombination) ) return false;

            TurtleModelCombination otherCombo = (TurtleModelCombination) other;
            return otherCombo.m_family == m_family &&
                otherCombo.m_colour == m_colour &&
                otherCombo.m_leftUpgrade == m_leftUpgrade &&
                otherCombo.m_rightUpgrade == m_rightUpgrade &&
                Objects.equal( otherCombo.m_overlay, m_overlay ) &&
                otherCombo.m_christmas == m_christmas &&
                otherCombo.m_flip == m_flip;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + m_family.hashCode();
            result = prime * result + (m_colour ? 1 : 0);
            result = prime * result + (m_leftUpgrade != null ? m_leftUpgrade.hashCode() : 0);
            result = prime * result + (m_rightUpgrade != null ? m_rightUpgrade.hashCode() : 0);
            result = prime * result + (m_overlay != null ? m_overlay.hashCode() : 0);
            result = prime * result + (m_christmas ? 1 : 0);
            result = prime * result + (m_flip ? 1 : 0);
            return result;
        }
    }

    private HashMap<TurtleModelCombination, IBakedModel> m_cachedModels;
    private ItemOverrideList m_overrides;
    private final TurtleModelCombination m_defaultCombination;

    public TurtleSmartItemModel()
    {
        m_cachedModels = new HashMap<>();
        m_defaultCombination = new TurtleModelCombination( ComputerFamily.Normal, false, null, null, null, false, false );
        m_overrides = new ItemOverrideList()
        {
            @Nonnull
            @Override
            public IBakedModel getModelWithOverrides( @Nonnull IBakedModel originalModel, @Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity )
            {
                ItemTurtle turtle = (ItemTurtle) stack.getItem();
                ComputerFamily family = turtle.getFamily();
                int colour = turtle.getColour( stack );
                ITurtleUpgrade leftUpgrade = turtle.getUpgrade( stack, TurtleSide.Left );
                ITurtleUpgrade rightUpgrade = turtle.getUpgrade( stack, TurtleSide.Right );
                ResourceLocation overlay = turtle.getOverlay( stack );
                boolean christmas = HolidayUtil.getCurrentHoliday() == Holiday.Christmas;
                String label = turtle.getLabel( stack );
                boolean flip = label != null && (label.equals( "Dinnerbone" ) || label.equals( "Grumm" ));
                TurtleModelCombination combo = new TurtleModelCombination( family, colour != -1, leftUpgrade, rightUpgrade, overlay, christmas, flip );
                if( m_cachedModels.containsKey( combo ) )
                {
                    return m_cachedModels.get( combo );
                }
                else
                {
                    IBakedModel model = buildModel( combo );
                    m_cachedModels.put( combo, model );
                    return model;
                }
            }
        };
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides()
    {
        return m_overrides;
    }

    @Override
    public void onResourceManagerReload( @Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate )
    {
        if( resourcePredicate.test( VanillaResourceType.MODELS ) ) m_cachedModels.clear();
    }

    private IBakedModel buildModel( TurtleModelCombination combo )
    {
        Minecraft mc = Minecraft.getInstance();
        ModelManager modelManager = mc.getItemRenderer().getItemModelMesher().getModelManager();
        ModelResourceLocation baseModelLocation = TileEntityTurtleRenderer.getTurtleModel( combo.m_family, combo.m_colour );
        ModelResourceLocation overlayModelLocation = TileEntityTurtleRenderer.getTurtleOverlayModel( combo.m_overlay, combo.m_christmas );
        IBakedModel baseModel = modelManager.getModel( baseModelLocation );
        IBakedModel overlayModel = (overlayModelLocation != null) ? modelManager.getModel( overlayModelLocation ) : null;
        Matrix4f transform = combo.m_flip ? s_flip : s_identity;
        Pair<IBakedModel, Matrix4f> leftModel = (combo.m_leftUpgrade != null) ? combo.m_leftUpgrade.getModel( null, TurtleSide.Left ) : null;
        Pair<IBakedModel, Matrix4f> rightModel = (combo.m_rightUpgrade != null) ? combo.m_rightUpgrade.getModel( null, TurtleSide.Right ) : null;
        if( leftModel != null && rightModel != null )
        {
            return new TurtleMultiModel( baseModel, overlayModel, transform, leftModel.getLeft(), leftModel.getRight(), rightModel.getLeft(), rightModel.getRight() );
        }
        else if( leftModel != null )
        {
            return new TurtleMultiModel( baseModel, overlayModel, transform, leftModel.getLeft(), leftModel.getRight(), null, null );
        }
        else if( rightModel != null )
        {
            return new TurtleMultiModel( baseModel, overlayModel, transform, null, null, rightModel.getLeft(), rightModel.getRight() );
        }
        else
        {
            return new TurtleMultiModel( baseModel, overlayModel, transform, null, null, null, null );
        }
    }

    // These should not be called:

    @Nonnull
    @Override
    public List<BakedQuad> getQuads( IBlockState state, EnumFacing facing, @Nonnull Random rand )
    {
        return getDefaultModel().getQuads( state, facing, rand );
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return getDefaultModel().isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return getDefaultModel().isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return getDefaultModel().isBuiltInRenderer();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return getDefaultModel().getParticleTexture();
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return getDefaultModel().getItemCameraTransforms();
    }

    private IBakedModel getDefaultModel()
    {
        IBakedModel model = m_cachedModels.get( m_defaultCombination );
        if( model == null )
        {
            model = buildModel( m_defaultCombination );
            m_cachedModels.put( m_defaultCombination, model );
        }

        return model;
    }
}
