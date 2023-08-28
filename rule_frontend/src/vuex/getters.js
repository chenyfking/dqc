import Utils from '@/common/utils'
import moment from 'moment'

export default {
  getFactById: state => (factId, fieldId) => {
    let fact = state.factMap[factId]
    if (!fact) return null
    const result = {
      id: fact.uuid,
      name: fact.name
    }
    if (fieldId) {
      result.fields = []
      fact.fields.some(f => {
        if (f.id === fieldId) {
          result.fields.push(f);
          return true
        }
      })
    } else {
      result.fields = fact.fields
    }
    return result
  },
  getAllFuncFact:(state)=>{
    let funcFact = {}
    funcFact.functions = state.functions
    funcFact.facts = state.facts
    return funcFact
  },
  getFactText: state => (factId, fieldId) => {
    if (!factId) return ''
    const fact = state.factMap[factId]
    if (!fact) return ''
    let textArr = [fact.name]
    if (fieldId) {
      if (fieldId.indexOf(',') == -1) {
        const field = state.fieldMap[factId + '.' + fieldId]
        if (field) textArr.push(field.label)
      } else {
        let tmpFactId = factId
        fieldId.split(',').some(fid => {
          const field = state.fieldMap[tmpFactId + '.' + fid]
          if (!field) return true
          textArr.push(field.label)
          if (!Utils.utils.isObject(field.type)) {
            return true
          }
          tmpFactId = field.subType
        })
      }
    }
    return textArr.join('.')
  },
  getConstantText: state => (constantId, fieldId) => {
    let text
    if (constantId || fieldId) {
      state.constants.some(constant => {
        if (constant.uuid == constantId) {
          if (fieldId) {
            constant.fields.some(field => {
              if (field.id == fieldId) {
                text = constant.name + '.' + field.label
                return true
              }
            })
          } else {
            text = constant.name
          }
          return true
        }
      })
    }
    return text
  },
  getModelName: state => modelId => {
    let name
    if (modelId) {
      state.models.some(e => {
        if (e.uuid == modelId) {
          name = e.modelName
          return true
        }
      })
    }
    return name
  },
  getFactField: state => (factId, fieldId) => {
    let field
    if (factId && fieldId) {
      if (fieldId.indexOf(',') == -1) {
        field = state.fieldMap[factId + '.' + fieldId]
      } else {
        let tmpFactId = factId
        fieldId.split(',').some(fid => {
          field = state.fieldMap[tmpFactId + '.' + fid]
          if (!field) return true
          if (!Utils.utils.isObject(field.type)) {
            return true
          }
          tmpFactId = field.subType
        })
      }
    }
    return field
  },
  getFieldText: state => (factId, fieldId) => {
    if (!factId) return ''
    const fact = state.factMap[factId]
    if (!fact) return ''
    let textArr = []
    if (fieldId) {
      if (fieldId.indexOf(',') == -1) {
        const field = state.fieldMap[factId + '.' + fieldId]
        if (field) textArr.push(field.label)
      } else {
        let tmpFactId = factId
        fieldId.split(',').some(fid => {
          const field = state.fieldMap[tmpFactId + '.' + fid]
          if (!field) return true
          textArr.push(field.label)
          if (!Utils.utils.isObject(field.type)) {
            return true
          }
          tmpFactId = field.subType
        })
      }
    }
    return textArr.join('.')
  },
  getConstantField: state => (constantId, fieldId) => {
    let field
    if (constantId && fieldId) {
      state.constants.some(e => {
        if (e.uuid === constantId) {
          e.fields.some(f => {
            if (f.id === fieldId) {
              field = f
              return true
            }
          })
          return true
        }
      })
    }
    return field
  },
  getFuncMethod: state => (funcName, methodName) => {
    let method
    if (funcName && methodName) {
      state.functions.some(f => {
        if (f.name == funcName) {
          f.methods.some(m => {
            if (m.name == methodName) {
              method = JSON.parse(JSON.stringify(m))
              return true
            }
          })
          return true
        }
      })
    }
    return method
  },
  getFactFields: state => factId => {
    let fields
    state.facts.some(e => {
      if (e.uuid === factId) {
        fields = e.fields
        return true
      }
    })
    return fields || []
  },
  getThirdApiName: state => apiId => {
    let text
    state.thirdapis.some(e => {
      if (e.uuid == apiId) {
        text = e.name
        return true
      }
    })
    return text
  },
  getExpressionType: (state, getters) => (expr, loopTarget) => {
    let type
    if (!expr || !expr.type) return null
    if (expr.type == 'INPUT') {
      type = 'String'
    } else if (expr.type == 'FACT' && expr.fact) {
      if (Utils.utils.isNotBlank(expr.fact.fieldId)) {
        const field = getters.getFactField(expr.fact.id, expr.fact.fieldId)
        if (field) {
          type = field.type
          if (Utils.utils.isCollection(type)) { type += '.' + field.subType }
          else if (type == 'Derive' && field.deriveData) {
            type = getters.getExpressionType(JSON.parse(field.deriveData))
          }
        }
      } else {
        const fact = state.factMap[expr.fact.id]
        if(fact) return 'Map'
      }

    } else if (expr.type == 'CONSTANT' && expr.constant) {
      const field = getters.getConstantField(expr.constant.id, expr.constant.fieldId)
      if (field) {
        type = field.type
      }
    } else if (expr.type == 'FUNC' && expr.func) {
      const method = getters.getFuncMethod(expr.func.name, expr.func.method)
      if (method) {
        type = method.returnType
      }
    } else if (expr.type == 'MODEL') {
      type = 'String'
    } else if (expr.type == 'COLLECTION_CHILD') {
      if (loopTarget) {
        let field = getters.getFactField(loopTarget.id, loopTarget.fieldId)
        if (field) {
          if (Utils.utils.isBaseType(field.subType)) type = field.subType
          else if (expr.collectionChild && expr.collectionChild.id) {
            field = getters.getFactField(expr.collectionChild.id, expr.collectionChild.fieldId)
            if (field) {
              type = field.type
            }
          }
        }
      }
    }
    return type
  },
  getFactExpressionByText: (state, getters) => text => {
    if (!text) return null
    const textArr = text.split('.')
    if (textArr.length <= 0) return null

    let fact
    const factName = textArr[0]
    state.facts.some(e => {
      if (factName == e.name) {
        fact = e
        return true
      }
    })
    if (!fact) return null

    const getFieldByLabel = (fact, label) => {
      let field
      fact.fields.some(e => {
        if (e.label === label) {
          field = e
          return true
        }
      })
      return field
    }

    let tmpFact = fact, fieldIdArr = []
    for (let i = 1; i < textArr.length; i++) {
      const field = getFieldByLabel(tmpFact, textArr[i])
      if (!field) return null

      fieldIdArr.push(field.id)
      if (Utils.utils.isObject(field.type)) {
        tmpFact = state.factMap[field.subType]
        if (!tmpFact) return null
      }
    }

    return {
      id: fact.uuid,
      fieldId: fieldIdArr.join(',')
    }
  },
  getFactDefaultValueById: (state, getters) => factId => {
    const fact = state.factMap[factId]
    if (fact) {
      return getters.getFactDefaultValue(fact)
    }
    return {}
  },
  getFactDefaultValue: (state, getters) => fact => {
    const json = {}
    fact.fields.forEach(field => {
      json[field.name] = getters.getFieldDefaultValue(field)
    })
    return json
  },
  getFieldDefaultValue: (state, getters) =>  field => {
    if (Utils.utils.isString(field.type)) {
      return ''
    } else if (Utils.utils.isNumber(field.type)) {
      return 0
    } else if (Utils.utils.isBoolean(field.type)) {
      return false
    } else if (Utils.utils.isDate(field.type)) {
      moment(new Date().getTime()).format("yyyy-MM-dd HH:mm:ss");
    } else if (Utils.utils.isCollection(field.type)) {
      if (Utils.utils.isObject(field.subType)) {
        return [ getters.getFieldDefaultValue({type: 'Map', subType: field.subType}) ]
      }
      return [ getters.getFieldDefaultValue({type: field.subType}) ]
    } else if (Utils.utils.isObject(field.type)) {
      const fact = state.factMap[field.subType]
      if (!fact) return null
      return getters.getFactDefaultValue(fact)
    }
  },
  userUuid: state => {
    const user = state.user
    return user != null ? user.uuid : null
  },
  userRealname: state => {
    const user = state.user
    if (user == null) return null
    if (user.realname) return user.realname
    return user.username
  }
}
