import { classNames } from '../utils/classNames';

export default function Tabs({ items, activeKey, onChange }) {
  return (
    <div className="tabs" role="tablist" aria-label="Section tabs">
      {items.map(item => (
        <button
          key={item.key}
          type="button"
          role="tab"
          aria-selected={activeKey === item.key}
          className={classNames('tabs__tab', activeKey === item.key && 'tabs__tab--active')}
          onClick={() => onChange(item.key)}
        >
          {item.label}
        </button>
      ))}
    </div>
  );
}
