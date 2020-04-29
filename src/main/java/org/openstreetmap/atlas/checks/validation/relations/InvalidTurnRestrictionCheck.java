package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.TurnRestriction;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.TurnRestrictionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Check for invalid turn restrictions
 *
 * @author gpogulsky
 */
public class InvalidTurnRestrictionCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Relation ID: {0,number,#} is marked as turn restriction, but it is not a well-formed relation (i.e. it is missing required members)",
            "Relation ID: {0,number,#} is marked as turn restriction, but one if it's members has no 'highway' tag");
    private static final long serialVersionUID = -983698716949386657L;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidTurnRestrictionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation && TurnRestrictionTag.isRestriction(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        if (TurnRestriction.from(relation).isEmpty())
        {
            final Set<AtlasObject> members = relation.members().stream()
                    .map(RelationMember::getEntity).collect(Collectors.toSet());
            return Optional.of(createFlag(members,
                    this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
        }
        else
        {
            Set<AtlasObject> membersWithNoHighwayTag = relation.membersOfType(ItemType.EDGE).stream()
                    .filter(member -> isRestrictionMember(member.getRole()))
                    .map(RelationMember::getEntity)
                    .filter(member -> member.getTag("highway").isEmpty())
                    .collect(Collectors.toSet());
            if (!membersWithNoHighwayTag.isEmpty()) {
                return Optional.of(createFlag(membersWithNoHighwayTag,
                        this.getLocalizedInstruction(1, relation.getOsmIdentifier())));
            }
            return Optional.empty();
        }
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private boolean isRestrictionMember(final String typeTag)
    {
        return typeTag.equals(RelationTypeTag.RESTRICTION_ROLE_FROM) || typeTag
                .equals(RelationTypeTag.RESTRICTION_ROLE_VIA) || typeTag.equals(RelationTypeTag.RESTRICTION_ROLE_TO);
    }
}
