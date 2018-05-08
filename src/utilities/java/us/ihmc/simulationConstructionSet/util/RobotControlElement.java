package us.ihmc.simulationConstructionSet.util;

import us.ihmc.yoVariables.registry.YoVariableRegistry;

public interface RobotControlElement
{
   public abstract void initialize();
   
   public abstract YoVariableRegistry getYoVariableRegistry();
   
   public abstract String getName();
   
   public abstract String getDescription();
}
