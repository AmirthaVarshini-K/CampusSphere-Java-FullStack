import Icon from './Icon';

export default function Timeline({ items = [] }) {
  return (
    <div className="timeline">
      {items.map(item => (
        <article key={item.title} className={`timeline__item timeline__item--${item.tone ?? 'neutral'}`}>
          <span className="timeline__icon">
            <Icon name={item.icon ?? 'clock'} size={16} />
          </span>
          <div className="timeline__copy">
            <div className="timeline__top">
              <strong>{item.title}</strong>
              {item.meta && <span>{item.meta}</span>}
            </div>
            <p>{item.description}</p>
          </div>
        </article>
      ))}
    </div>
  );
}
