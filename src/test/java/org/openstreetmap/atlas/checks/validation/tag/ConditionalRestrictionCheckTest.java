package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;


/**
 * @author laura on 09/06/2020.
 */
public class ConditionalRestrictionCheckTest {

    @Rule
    public ConditionalRestrictionCheckTestRule setup = new ConditionalRestrictionCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void validRestriction() {
        verifier.actual(setup.getConditionalWay(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidConditionalKey() {
        verifier.actual(setup.getInvalidConditionalKey(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "The conditional key hgv:maxweight:conditional does not respect the \"<restriction-type>[:<transportation mode>][:<direction>]:conditional\" format")));
    }

    @Test
    public void invalidConditionFormat() {
        verifier.actual(setup.getInvalidConditionFormat(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "The conditional value no@(Sa 08:00-16:00) does not respect the format \"<restriction-value> @ <condition>[;<restriction-value> @ <condition>]\" ")));

    }

    @Test
    public void invalidAccessType() {
        verifier.actual(setup.getInvalidAccessType(),
                new ConditionalRestrictionCheck(ConfigurationResolver.emptyConfiguration()));
        verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "The conditional value notallowed @ (Sa 08:00-16:00) does not respect the format \"<restriction-value> @ <condition>[;<restriction-value> @ <condition>]\" ")));

    }
}