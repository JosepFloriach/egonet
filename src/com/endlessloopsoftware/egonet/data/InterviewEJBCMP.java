/*
 * Generated by XDoclet - Do not edit!
 */
package com.endlessloopsoftware.egonet.data;

/**
 * CMP layer for InterviewEJB.
 */
public abstract class InterviewEJBCMP
   extends com.endlessloopsoftware.egonet.ejb.InterviewEJB
   implements javax.ejb.EntityBean
{

   public void ejbLoad() throws javax.ejb.EJBException
   {
      super.ejbLoad();
   }

   public void ejbStore() throws javax.ejb.EJBException
   {
         super.ejbStore();
   }

   public void ejbActivate() throws javax.ejb.EJBException
   {
      super.ejbActivate();
   }

   public void ejbPassivate() throws javax.ejb.EJBException
   {
      super.ejbPassivate();

   }

   public void setEntityContext(javax.ejb.EntityContext ctx) throws javax.ejb.EJBException
   {
      super.setEntityContext(ctx);
   }

   public void unsetEntityContext() throws javax.ejb.EJBException
   {
      super.unsetEntityContext();
   }

   public void ejbRemove() throws javax.ejb.EJBException, javax.ejb.RemoveException
   {
      super.ejbRemove();

   }

 /* Value Objects BEGIN */

/* Value Objects END */

   public abstract java.lang.Long getId() ;

   public abstract void setId( java.lang.Long id ) ;

   public abstract java.lang.String getFirstName() ;

   public abstract void setFirstName( java.lang.String firstName ) ;

   public abstract java.lang.String getLastName() ;

   public abstract void setLastName( java.lang.String lastName ) ;

   public abstract java.lang.Boolean getComplete() ;

   public abstract void setComplete( java.lang.Boolean complete ) ;

   public abstract java.lang.String[] getAlters() ;

   public abstract void setAlters( java.lang.String[] alters ) ;

   public abstract int[][] getAdjacencyMatrix() ;

   public abstract void setAdjacencyMatrix( int[][] adjacencyMatrix ) ;

}