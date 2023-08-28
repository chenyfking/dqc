import { Message } from 'element-ui'

const utils = {
    assetsArr: [
        { type: 'fact', name: '数据结构', icon: 'iconfont iconshujumoxing', add: true },
        { type: 'constant', name: '枚举常量', icon: 'iconfont iconxinzengchangliang', add: true },
        { type: 'guidedrule', name: '向导式决策集', icon: 'iconfont iconweibiaoti-', add: true },
        { type: 'dir', name: '文件夹', icon: 'el-icon-folder-add', add: true },
        { type: 'pkg', name: '知识包', icon: 'el-icon-suitcase', add: false },
        { type: 'search', name: '搜索结果', icon: 'el-icon-search', add: false },
        { type: 'recycle', name: '回收站', icon: 'el-icon-delete', add: false }
    ],
    isNumber: type => {
        return type == 'Integer' ||
            type == 'Long' ||
            type == 'Double' ||
            type == 'BigDecimal' ||
            type == 'Number'
    },
    isBoolean: type => {
        return type == 'Boolean'
    },
    isString: type => {
        return type == 'String'
    },
    isDate: type => {
        return type == 'Date'
    },
    isList: type => {
        if (utils.isBlank(type)) return false
        if (typeof type === 'string' && type.startsWith('List')) return true
        if (typeof type === 'object' && type.type === 'List') return true
        return false
    },
    isSet: type => {
        if (utils.isBlank(type)) return false
        if (typeof type === 'string' && type.startsWith('Set')) return true
        if (typeof type === 'object' && type.type === 'Set') return true
        return false
    },
    isDerive: type => {
        return type == 'Derive'
    },
    isCollection: type => {
        return utils.isList(type) || utils.isSet(type)
    },
    isObject: type => {
        return type && (type === 'Map' || type === 'Object' || type.length == 32)
    },
    isTypeMatch: (type1, type2) => {
        if (!type1 || !type2) return false

        type1 = type1.trim()
        type2 = type2.trim()
        if (type1 == type2) return true
        if (utils.isCollection(type1) && utils.isCollection(type2)) return true
        if (utils.isNumber(type1) && utils.isNumber(type2)) return true
        if (utils.isObject(type1) && utils.isObject(type2)) return true
        if (type1 == 'Collection' && (type2 == 'List' || type2 == 'Set')) return true
        if (type2 == 'Collection' && (type1 == 'List' || type1 == 'Set')) return true
        return false
    },
    isTypeNotMatch: (type1, type2) => {
        return !utils.isTypeMatch(type1, type2)
    },
    getFieldType: field => {
        if (utils.isList(field.type) || utils.isSet(field.type)) {
            return { type: field.type, subType: field.subType }
        }
        return field.type
    },
    getAssetsTypeText: function(type) {
        return this.getAssetsByType(type).name
    },
    getAssetsTypeIcon: function(type) {
        return this.getAssetsByType(type).icon
    },
    getAssetsByType: function(type) {
        let assets;
        this.assetsArr.some(e => {
            if (e.type == type) {
                assets = e
                return true
            }
        })
        return assets
    },
    isBlank: value => {
        if (value == null) return true
        if (typeof value == 'string' && value.trim() === '') return true
        return false
    },
    isNotBlank: value => {
        return !utils.isBlank(value)
    },
    isAnyBlank() {
        for (let i = 0; i < arguments.length; i++) {
            if (utils.isBlank(arguments[i])) {
                return true
            }
        }
        return false
    },
    randomCode: len => {
        let str = ''
        for (let i = 0; i < len; i++) {
            const r = parseInt(Math.random() * (61 + 1), 10)
            str += 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'.charAt(r)
        }
        return str
    },
    // 'Map', 'Derive'
    fieldTypeArr: [
        'String', 'Integer', 'Double', 'Long', 'BigDecimal', 'Boolean', 'Date', 'List', 
    ],
    fieldBaseTypeArr: [
        'String', 'Integer', 'Double', 'Long', 'BigDecimal', 'Boolean', 'Date',
    ],
    isBaseType: type => {
        return utils.fieldBaseTypeArr.indexOf(type) != -1 || utils.isNumber(type)
    },
    log: (msg) => {
        console.log(msg)
    },
    equals: (obj1, obj2) => {
        if (typeof obj1 === 'string' && typeof obj2 === 'string' && obj1.trim() === obj2.trim()) {
            return true
        }
        if (typeof obj1 === 'object' && typeof obj2 === 'object' &&
            JSON.stringify(obj1) === JSON.stringify(obj2)) {
            return true
        }
        return obj1 === obj2
    },
    copy: obj => {
        return obj ? JSON.parse(JSON.stringify(obj)) : {}
    },
    unique: (array) => {
        return Array.from(new Set(array))
    },
    simpleClone: (obj) => {
        var copyObj = {}
        for (var i in obj) {
            copyObj[i] = obj[i]
        }
        return copyObj
    },
    clearTmpKey: json => {
        if (!json) return
        if (Array.isArray(json)) {
            json.forEach(e => utils.clearTmpKey(e))
        } else {
            Object.keys(json).forEach(e => {
                if (e.startsWith('_')) {
                    delete json[e]
                } else {
                    if (Array.isArray(json[e])) {
                        json[e].forEach(e2 => {
                            utils.clearTmpKey(e2)
                        })
                    } else if (typeof json[e] === 'object') {
                        utils.clearTmpKey(json[e])
                    }
                }
            })
        }
    },
    arrayFind: (array, callback) => {
        if (typeof array.find === 'function') {
            return array.find(callback)
        }
        const result = array.filter(callback)
        if (result.length > 0) {
            return result[0]
        }
        return null
    },
    arrayFindIndex: (array, callback) => {
        if (typeof array.findIndex === 'function') {
            return array.findIndex(callback)
        }
        let idx = -1
        array.some((e, i) => {
            if (callback(e)) {
                idx = i
                return true
            }
        })
        return idx
    },
    arrayFlat: array => {
        if (typeof array.flat === 'function') {
            return array.flat()
        }
        const newArr = []
        array.forEach(e => {
            if (Array.isArray(e)) {
                utils.arrayFlat(e).forEach(e2 => {
                    newArr.push(e2)
                })
            } else {
                newArr.push(e)
            }
        })
        return newArr
    },
    getCollectionSubType: type => {
        const dotIndex = type.indexOf('.')
        if (dotIndex != -1) {
            return type.substring(dotIndex + 1)
        }
        return null
    },
    checkUploadFile: (e, type) => {
        const maxUploadSize = 30
        if (e.size > maxUploadSize * 1024 * 1024) {
            Message({ type: 'error', message: `上传文件大小不能超过${maxUploadSize}MB`, showClose: true })
            return false
        }
        if (type) {
            let isSupportType = false
            if (Array.isArray(type)) {
                type.some(t => {
                    if (e.name.endsWith('.' + t)) {
                        isSupportType = true
                        return true
                    }
                })
            } else {
                isSupportType = e.name.endsWith('.' + type)
            }
            if (!isSupportType) {
                Message({ type: 'error', message: '不支持的文件类型', showClose: true })
                return false
            }
        }
        return true
    }
}

const install = function(Vue, opts = {}) {
    Vue.prototype.$utils = utils
}

export default {
    install,
    utils
}