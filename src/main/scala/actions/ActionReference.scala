package com.originate.scalypher.action

import com.originate.scalypher.path.Path
import com.originate.scalypher.ToQueryWithIdentifiers
import com.originate.scalypher.types.NodeOrRelationship
import com.originate.scalypher.types.ReferenceableMap
import com.originate.scalypher.util.Exceptions.IdentifierAliasCollisionException
import com.originate.scalypher.util.Exceptions.IdentifierDoesntExistException
import com.originate.scalypher.where.ObjectReference
import com.originate.scalypher.where.NodeOrRelationshipReference

sealed trait ActionReference extends ToQueryWithIdentifiers {

  val as: Option[String]

  def as(name: String): ActionReference

  def toColumn(referenceableMap: ReferenceableMap): String

  protected def asString(referenceableMap: ReferenceableMap): Option[String] =
    as map { name =>
      if (referenceableMap.values.toSet contains name)
        throw new IdentifierAliasCollisionException(name)
      s"AS $name"
    }

}

case class ActionPath(
  path: Path,
  as: Option[String] = None
) extends ActionReference {

  def as(name: String): ActionPath =
    copy(as = Some(name))

  def toQuery(referenceableMap: ReferenceableMap): String =
    referenceableMap get path map { identifier =>
      Seq(Some(identifier), asString(referenceableMap)).flatten mkString " "
    } getOrElse (throw new IdentifierDoesntExistException())

  def toColumn(referenceableMap: ReferenceableMap): String =
    as orElse referenceableMap.get(path) getOrElse (throw new IdentifierDoesntExistException())

}

case class ActionNodeOrRelationship(
  reference: NodeOrRelationshipReference,
  as: Option[String] = None
) extends ActionReference {

  def as(name: String): ActionNodeOrRelationship =
    copy(as = Some(name))

  def toQuery(referenceableMap: ReferenceableMap): String =
    Seq(Some(reference.toQuery(referenceableMap)), asString(referenceableMap)).flatten mkString " "

  def toColumn(referenceableMap: ReferenceableMap): String =
    as getOrElse reference.toQuery(referenceableMap)

}
