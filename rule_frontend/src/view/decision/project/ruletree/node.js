export default class Node {
  constructor(context, parentNode, data = {}) {
    this.context = context;
    this.parentNode = parentNode;
    this.data = data;
    this.children = [];
  }

  mount(vueInstance) {
    const vm = vueInstance.$mount();
    this.context.container.appendChild(vm.$el);
    this.vm = vm;
    this.dom = vm.$el;
    if (this.context.readonly) {
      this.vm.readonly = this.context.readonly;
    }

    this.initChildren();
  }

  initChildren() {
    if (!this.data.children || this.data.children.length == 0) return;

    this.data.children.forEach(child => {
      this.addChild(child);
    })
  }

  addChild(child) {
    const childNode = this.context.newNode(child, this);
    this.children.push(childNode);
    this.context.traverse();
    return childNode;
  }

  repeatNode() {
    const index = this.parentNode.children.indexOf(this);
    if (index != -1) {
      const childNode = this.context.newNode(JSON.parse(JSON.stringify(this.data)), this.parentNode);
      this.parentNode.children.splice(index, 0, childNode);
      this.context.traverse();
    }
  }

  delete() {
    this.deleteRecursively();
    this.context.traverse();
  }

  deleteRecursively() {
    while (this.children.length > 0) {
      this.children[this.children.length - 1].deleteRecursively();
    }

    if (!this.isRootNode()) {
      const index = this.parentNode.children.indexOf(this);
      this.parentNode.children.splice(index, 1);
      this.parentNode.data.children.splice(index, 1);
      this.vm.$destroy();
      this.dom.remove();
      this.connection.path.remove();
      this.context.deleteNode(this);
    }
  }

  isRootNode() {
    return this == this.context.rootNode;
  }

  traverse() {
    this.w = this.dom.offsetWidth;
    this.h = this.dom.offsetHeight;

    if (!this.parentNode) {
      this.x = 0;
    } else {
      this.x = this.parentNode.x + this.parentNode.w + 10;
    }

    if (this.children.length == 0) {
      if (this.context.height > 0) {
        this.y = this.context.height + 10;
      } else {
        this.y = this.context.height;
      }
      this.context.height = this.y + this.h;
    } else {
      this.children.forEach(child => {
        child.traverse();
      })
      const firstChild = this.children[0];
      const lastChild = this.children[this.children.length - 1];
      // 父节点Y坐标居中
      this.y = firstChild.y + (lastChild.y + lastChild.h - firstChild.y - this.h) / 2 ;
    }

    if (this.x < 0) {
      this.x = 0;
    }
    if (this.y < 0) {
      this.y = 0;
    }

    const newContextWidth = this.x + this.w;
    if (newContextWidth > this.context.width) {
      this.context.width = newContextWidth;
    }
  }

  setPosition() {
    this.dom.style.left = this.x + 'px';
    this.dom.style.top = this.y + 'px';
  }

  copyNode() {
    this.context.clipboard = {
      node: this,
      type: 'COPY'
    }
  }

  cutNode() {
    this.context.clipboard = {
      node: this,
      type: 'CUT'
    }
  }

  pasteNode() {
    if (!this.context.clipboard) return;

    const type = this.context.clipboard.type;
    const node = this.context.clipboard.node;
    const data = JSON.parse(JSON.stringify(node.data));

    this.context.startTransaction(() => {
      if (type == 'CUT') {
        const isOnThePath = this.isOnThePath(node);
        if (isOnThePath) {
          return '不能剪切当前节点所在分支';
        }
        node.delete();
        this.context.clipboard = null;
      }
      this.addChild(data);
    })
  }

  isOnThePath(node) {
    if (this == node) return true;

    let onThePath = false;
    if (node.children.length > 0) {
      node.children.some(child => {
        onThePath = this.isOnThePath(child)
        if (onThePath) return true
      })
    }
    return onThePath
  }

  destroy() {
    this.vm.$destroy();
  }
}
