<template>
  <div>
    <fact :input-type="{type: 'List', subType: 'Object'}" v-model="params[0].assign" @change="changeFact"></fact>
    <span v-if="params.length > 1">,&nbsp;</span>
    <template v-for="(e, i) in params" v-if="i > 0">
      <dropdown @command="command => setParamType(i, command)" v-if="getInputType(e.type).length != 32">
        <span class="el-dropdown-link" style="color: darkcyan">{{e.name}}</span>
        <dropdown-menu slot="dropdown">
          <dropdown-item command="input">输入值</dropdown-item>
          <dropdown-item command="fact">数据结构</dropdown-item>
          <dropdown-item command="constant">枚举常量</dropdown-item>
        </dropdown-menu>
      </dropdown>
      <span v-else style="color: darkcyan; font-size: 14px;">{{e.name}}</span>
      <template v-if="e.assign">
        <span v-if="e.assign.type">:</span>
        <span v-if="subType.length == 32" style="color: rgb(180, 95, 4)">
          {{$store.getters.getFactById(e.assign.value.id).name}}
        </span>
        <valueInput v-else-if="e.assign.type == 'input'" :input-type="getInputType(e.type)"
                    v-model="e.assign.value"></valueInput>
        <fact v-else-if="e.assign.type == 'fact'" :input-type="getChildInputType(e.type)"
              v-model="e.assign.value" :select-any="getChildInputType(e.type).length == 32"></fact>
        <constant v-else-if="e.assign.type == 'constant'" :input-type="getInputType(e.type)"
                  v-model="e.assign.value"></constant>
      </template>
      {{params.length - 2 > i ? ',&nbsp;' : ''}}
    </template>
  </div>
</template>

<script>
  export default {
    name: "func-list",
    props: {
      value: Array,
      type: String
    },
    data() {
      return {
        params: this.initParams(this.value)
      }
    },
    computed: {
      subType() {
        if (this.params[0].assign.value) {
          const fact = this.$store.getters.getFactById(this.params[0].assign.value.id, this.params[0].assign.value.field.id)
          return fact && fact.fields.length > 0 ? fact.fields[0].subType : ''
        }
        return ''
      },
      subChildType() {
        if (this.params[0].assign) {
          const fact = this.$store.getters.getFactById(this.params[0].assign.id, this.params[0].assign.field.id)
          return fact && fact.fields.length > 0 ? fact.fields[0].subType : ''
        }
        return ''
      },
    },
    watch: {
      params: {
        deep: true,
        handler: function(newVal) {
          this.$emit('input', newVal)
        }
      },
      value: {
        deep: true,
        handler: function(newVal, oldVal) {
          if (JSON.stringify(newVal) != JSON.stringify(oldVal)) {
            this.params = this.initParams(newVal)
          }
        }
      }
    },
    methods: {
      initParams(value) {
        if (!value[0].assign) {
          value[0].assign = {type: 'fact'}
        }
        return JSON.parse(JSON.stringify(value))
      },
      getInputType(type) {
        return type == 'Object' ? this.subType : type
      },
      getChildInputType(type) {
        var type =  type == 'Object' ? this.subChildType : type
        return type
      },
      setParamType(i, type) {
        let param = {...this.params[i], assign: {type: type, value: {}}}
        this.$set(this.params, i, param)
      },
      changeFact(command) {
        if (this.params.length > 1) {
          const fact = this.$store.getters.getFactById(command.id, command.field.id)
          this.params.forEach((e, i) => {
            if (i > 0) {
              if (fact.fields[0].subType.length == 32) {
                this.params[i].assign = {
                  type: 'fact',
                  value: {
                    id: fact.fields[0].subType
                  }
                }
              } else {
                this.$delete(this.params[i], 'assign')
              }
            }
          })
        }
      }
    }
  }
</script>

<style scoped>

</style>
