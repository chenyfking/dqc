const map = {};
let seed = 0;
let showSeed = -1;

const menuEl = document.createElement('ul');
document.body.appendChild(menuEl);
menuEl.className = 'el-dropdown-menu el-popper';
menuEl.style.transformOrigin = 'center bottom 0px';

menuEl.addEventListener("click", handleClickMenu, false);
document.body.addEventListener("click", hideMenu, false);

function showMenu(e, i) {
  const options = map[i];

  const items = typeof options.items === 'function' ? options.items() : options.items;
  const cache = [];

  let html = '';
  items.forEach(item => {
    if (item.filter === false || (typeof item.filter === 'function' && !item.filter(item))) {
      return true;
    }

    html += '<li class="el-dropdown-menu__item';
    if (item.disabled === true || (typeof item.disabled === 'function' && item.disabled(item))) {
      html += ' is-disabled';
    }
    if (item.divided) {
      html += ' el-dropdown-menu__item--divided';
    }
    html += '">';
    if (item.icon) {
      html += `<i class="${item.icon}"></i> ${item.text}`
    } else {
      html += item.text
    }
    html += '</li>';

    cache.push(item);
  });
  menuEl.innerHTML = html;

  displayMenu(e);
  showSeed = i;
  options.cache = cache;
}

function displayMenu(e) {
  let scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
  let scrollY = document.documentElement.scrollTop || document.body.scrollTop;
  let x = e.pageX || e.clientX + scrollX;
  let y = e.pageY || e.clientY + scrollY;
  menuEl.style.display = 'block';
  if (x + menuEl.offsetWidth > document.body.scrollWidth) {
    x -= menuEl.offsetWidth;
  }
  if (y + menuEl.offsetHeight + 10 > document.body.scrollHeight) {
    y -= menuEl.offsetHeight;
  }
  menuEl.style.left = x + menuEl.style.width + 'px';
  menuEl.style.top = y + 'px';
}

function hideMenu(e) {
  if (e.target != menuEl) {
    menuEl.style.display = 'none';
  }
}

function handleClickMenu(e) {
  e.stopPropagation();

  const target = e.target;
  if (target.tagName.toLocaleLowerCase() == 'li') {
    const children = menuEl.children;
    for (let i = 0, len = children.length; i < len; i++) {
      if (children[i] == target) {
        const options = map[showSeed];
        const item = options.cache[i];
        if (typeof item.click === 'function') {
          item.click(item.command);
        }
      }
    }
    menuEl.style.display = 'none';
  }
}

function handleContextmenuEvent(e) {
  showMenu(e, this.seed);
  e.preventDefault();
  return false;
}

function getOptions(binding) {
  const options = binding.value;
  if (!options || !options.items) return null;
  if (typeof options.items !== 'function' && options.items.length == 0) return null;
  return options;
}

export default {
  bind: function (el, binding, vnode) {
    const options = getOptions(binding);
    if (options != null) {
      el.seed = seed++;
      map[el.seed] = options;
      el.addEventListener('contextmenu', handleContextmenuEvent, true);
    }
  },
  update: function (el, binding, vnode) {
    const options = getOptions(binding);
    if (options != null) {
      map[el.seed] = options;
    }
  },
  unbind: function (el, binding, vnode) {
    delete map[el.seed];
    el.removeEventListener('contextmenu', handleContextmenuEvent, true);
  }
}
