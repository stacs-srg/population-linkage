package uk.ac.standrews.cs.population_linkage.experiments.linkage;

import uk.ac.standrews.cs.storr.impl.LXPMetadata;
import uk.ac.standrews.cs.storr.impl.StaticLXP;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.types.LXPBaseType;
import uk.ac.standrews.cs.storr.types.LXP_LIST;
import uk.ac.standrews.cs.storr.types.LXP_REF;
import uk.ac.standrews.cs.storr.types.LXP_SCALAR;

public class LXPLink extends StaticLXP {

    @LXP_REF(type = "lxp")
    public static int ref1;
    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ROLE1_TYPE;
    @LXP_REF(type = "lxp")
    public static int ref2;
    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int ROLE2_TYPE;
    @LXP_SCALAR(type = LXPBaseType.DOUBLE)
    public static int confidence;
    @LXP_SCALAR(type = LXPBaseType.STRING)
    public static int link_type;
    @LXP_LIST(basetype = LXPBaseType.STRING)
    public static int provenance;
    private static LXPMetadata static_metadata;

    static {
        try {
            static_metadata = new LXPMetadata(LXPLink.class, "LXPLink");
        } catch (Exception var1) {
            throw new RuntimeException(var1);
        }
    }

    public LXPLink() {

    }

    public LXPLink(Link link) throws BucketException {

        this.put(ref1, link.getRecord1().getReferend().getId());
        this.put(ROLE1_TYPE, link.getRole1());
        this.put(ref2, link.getRecord2().getReferend().getId());
        this.put(ROLE2_TYPE, link.getRole2());
        this.put(confidence, link.getConfidence());
        this.put(link_type, link.getLinkType());
        this.put(provenance, link.getProvenance());
    }

    @Override
    public LXPMetadata getMetaData() {
        return static_metadata;
    }
}