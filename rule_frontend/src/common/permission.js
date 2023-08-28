import store from '../vuex'

function impliesOne(thisPermission, otherPermission) {
  const thisParts = getParts(thisPermission)
  const otherParts = getParts(otherPermission)
  let k = 0

  for (let i = 0, len = otherParts.length; i < len; i++) {
    const otherPart = otherParts[i]

    if (thisParts.length - 1 < k) {
      return true
    } else {
      const part = thisParts[k]
      if (part.indexOf("*") == -1 && !containsAll(part, otherPart)) {
        return false
      }
      k++;
    }
  }

  while (k < thisParts.length) {
    const part = thisParts[k]
    if (part.indexOf("*") == -1) {
      return false
    }
    k++
  }
  return true
}

function getParts(permission){
  permission = permission.trim().toLocaleLowerCase()
  const thisParts = []
  permission.split(':').forEach(e => {
    thisParts.push(e.split(','))
  })
  return thisParts
}

function containsAll(arr1, arr2) {
  for (let i = 0, len = arr2.length; i < len; i++) {
    if (arr1.indexOf(arr2[i]) == -1) return false
  }
  return true
}

export function hasPerm(permission){
    const user = store.state.user
    if (!user) return false
    if (user.username == 'superadmin') return true
    if (!user.permissions) return false

    for (let i = 0; i < user.permissions.length; i++) {
        if (impliesOne(user.permissions[i], permission)) {
            return true
        }
    }
    return false
}

export function hasAnyPerm(...permissions) {
  let flag = false
  permissions.some(e => {
    if (hasPerm(e)) {
      flag = true
      return true
    }
  })
  return flag
}
