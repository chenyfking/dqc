<template>
  <div style="display: inline-block;">
    <dropdown @command="change" ref="dropdown" v-if="!readonly">
      <span class="el-dropdown-link" style="color: darkcyan; padding: 0px;">{{text}}</span>
      <dropdown-menu slot="dropdown" v-if="$store.state.thirdapis.length > 0">
        <dropdown-item v-for="(e, i) in $store.state.thirdapis" :key="i" :command="e.uuid">{{e.name}}</dropdown-item>
      </dropdown-menu>
      <dropdown-menu slot="dropdown" v-else>
        <dropdown-item disabled>无</dropdown-item>
      </dropdown-menu>
    </dropdown>
    <span v-else style="color: darkcyan; font-size: 14px;">{{text}}</span>

    <template v-if="value.id">
      (
      <span style="color: darkcyan; font-size: 14px;" v-if="!value.input">输入: </span>
      <fact v-model="value.input" input-type="Object" :readonly="readonly"></fact>
      ,
      <span style="color: darkcyan; font-size: 14px;" v-if="!value.output">输出: </span>
      <fact v-model="value.output" input-type="Object" :readonly="readonly"></fact>
      )
    </template>
  </div>
</template>

<script>
  export default {
    name: "thirdapi",
    props: {
      value: {
        type: Object,
        default: function() {
          return {}
        }
      },
      immediate: Boolean,
      readonly: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      text: function() {
        return this.$store.getters.getThirdApiName(this.value.id) || '请选择'
      }
    },
    mounted() {
      if (this.immediate) {
        this.$refs.dropdown.show()
      }
    },
    methods: {
      change(command) {
        this.$set(this.value, 'id', command)
        this.$emit('input', this.value)
      }
    }
  }
</script>

<style scoped>

</style>
