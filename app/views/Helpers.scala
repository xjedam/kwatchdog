package views

object Auth {
  def hasPermission(u: model.User, minRole: String): Boolean = {
    model.User.roles.get(u.role).get >= model.User.roles.get(minRole).get
  }
}